import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

const config: Config = {
  title: 'Gestalt Config',
  tagline: 'A Java configuration library',
  favicon: 'img/favicon.ico',

  // Set the production url of your site here
  url: 'https://gestalt-config.github.io',
  // Set the /<baseUrl>/ pathname under which your site is served
  // For GitHub pages deployment, it is often '/<projectName>/'
  baseUrl: '/gestalt/',

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: 'gestalt-config', // Usually your GitHub org/user name.
  projectName: 'gestalt', // Usually your repo name.
  trailingSlash: false,
  deploymentBranch: 'gh-pages',

  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',

  // Even if you don't use internationalization, you can use this field to set
  // useful metadata like html lang. For example, if your site is Chinese, you
  // may want to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      {
        docs: {
          sidebarPath: './sidebars.ts',
          // Please change this to your repo.
          // Remove this to remove the "edit this page" links.
          editUrl: 'https://github.com/gestalt-config/gestalt/tree/main/docs/gestalt-static',
        },
        blog: {
          showReadingTime: true,
          feedOptions: {
            type: ['rss', 'atom'],
            xslt: true,
          },
          // Please change this to your repo.
          // Remove this to remove the "edit this page" links.
          editUrl:
            'https://github.com/gestalt-config/gestalt/tree/main/docs/gestalt-static',
          // Useful options to enforce blogging best practices
          onInlineTags: 'warn',
          onInlineAuthors: 'warn',
          onUntruncatedBlogPosts: 'warn',
        },
        theme: {
          customCss: './src/css/custom.css',
        },
        gtag: {
          trackingID: 'G-JK529KMZG0',
          anonymizeIP: true,
        },
      } satisfies Preset.Options,
    ],
  ],

  themeConfig: {
    // Replace with your project's social card
    image: 'img/docusaurus-social-card.jpg',
    navbar: {
      title: 'Gestalt Config',
      logo: {
        alt: 'My Site Logo',
        src: 'img/logo.svg',
      },
      items: [
        {
          type: 'docSidebar',
          sidebarId: 'tutorialSidebar',

          position: 'left',
          label: 'Tutorial - Basics',
        },
        {
          type: 'docSidebar',
          sidebarId: 'advancedSidebar',

          position: 'left',
          label: 'Tutorial - Advanced',
        },
        {
          type: 'docSidebar',
          sidebarId: 'modulesSidebar',

          position: 'left',
          label: 'Modules',
        },
        {
          type: 'docSidebar',
          sidebarId: 'usecaseSidebar',

          position: 'left',
          label: 'Use Cases',
        },
        {
          type: 'docSidebar',
          sidebarId: 'architectureSidebar',

          position: 'left',
          label: 'Architecture',
        },
        {
          href: 'https://github.com/gestalt-config/gestalt',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Docs',
          items: [
            {
              label: 'Tutorial - Basic',
              to: '/docs/tutorial/getting-started',
            },
           {
              label: 'Tutorial - Advanced',
              to: '/docs/advanced/reload-strategies',
            },
          ],
        },
        {
          title: 'Community',
          items: [
            {
              label: 'Github Discussions',
              href: 'https://github.com/gestalt-config/gestalt/discussions',
            },
            {
              label: 'Github Issues',
              href: 'https://github.com/gestalt-config/gestalt/issues',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} Gestalt Config. Built with Docusaurus.`,
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
    },
  } satisfies Preset.ThemeConfig,
};

export default config;
